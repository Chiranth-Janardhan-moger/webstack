import urllib.request
import urllib.error
import json
import os
import sys
import time
import mimetypes

gitlab_token = os.environ.get("GITLAB_PAT")
project_id = os.environ.get("GITLAB_PROJECT_ID")
api_url = os.environ.get("GITLAB_API_URL", "https://gitlab.com/api/v4")
tag_name = os.environ.get("RELEASE_TAG")

if not gitlab_token or not project_id or not tag_name:
    print("Warning: Missing required environment variables (GITLAB_PAT, GITLAB_PROJECT_ID, RELEASE_TAG). Skipping GitLab upload.")
    sys.exit(0)

api_url = api_url.rstrip('/')

files_to_upload = sys.argv[1:]
if not files_to_upload:
    print("Error: No files specified for upload.", file=sys.stderr)
    sys.exit(1)

headers = {
    "PRIVATE-TOKEN": gitlab_token,
    "User-Agent": "GitHub-Actions-CD-Pipeline"
}

def upload_file_with_retry(file_path, retries=3, delay=5):
    """Uploads a file to GitLab Uploads API with retries on transient errors."""
    if not os.path.exists(file_path):
        print(f"Error: File '{file_path}' does not exist.", file=sys.stderr)
        return None

    filename = os.path.basename(file_path)
    mime_type, _ = mimetypes.guess_type(file_path)
    if not mime_type:
        mime_type = "application/octet-stream"

    boundary = f"----GitHubActionsBoundary{int(time.time())}"
    header_bytes = (
        f"--{boundary}\r\n"
        f"Content-Disposition: form-data; name=\"file\"; filename=\"{filename}\"\r\n"
        f"Content-Type: {mime_type}\r\n\r\n"
    ).encode('utf-8')
    footer_bytes = f"\r\n--{boundary}--\r\n".encode('utf-8')

    with open(file_path, 'rb') as f:
        file_data = f.read()

    body = b"".join([header_bytes, file_data, footer_bytes])

    upload_headers = {
        **headers,
        "Content-Type": f"multipart/form-data; boundary={boundary}",
        "Content-Length": str(len(body))
    }

    url = f"{api_url}/projects/{project_id}/uploads"

    for attempt in range(retries):
        try:
            print(f"Uploading '{filename}' to GitLab (Attempt {attempt+1}/{retries})...")
            req = urllib.request.Request(url, data=body, headers=upload_headers, method="POST")
            with urllib.request.urlopen(req) as res:
                response_data = json.loads(res.read().decode('utf-8'))
                print(f"Successfully uploaded '{filename}'. Link: {response_data['url']}")
                return response_data
        except urllib.error.HTTPError as e:
            err_msg = e.read().decode('utf-8')
            print(f"HTTP Error {e.code} during upload: {err_msg}", file=sys.stderr)
            if e.code >= 500:
                time.sleep(delay * (attempt + 1))
            else:
                break
        except Exception as e:
            print(f"Network error during upload: {e}", file=sys.stderr)
            time.sleep(delay * (attempt + 1))

    print(f"Error: Failed to upload '{filename}' after {retries} attempts.", file=sys.stderr)
    return None

def create_or_update_release(links):
    """Creates or updates a GitLab release with the uploaded asset links."""
    release_url = f"{api_url}/projects/{project_id}/releases"
    
    release_data = {
        "name": f"Release {tag_name}",
        "tag_name": tag_name,
        "description": f"WebStack release version {tag_name} compiled automatically via CI/CD pipeline.\n\n### SHA256 Verification\nVerify downloaded APK binaries against the provided SHA256 checksums.",
        "assets": {
            "links": links
        }
    }

    try:
        print(f"Creating GitLab release for tag '{tag_name}'...")
        req = urllib.request.Request(
            release_url,
            data=json.dumps(release_data).encode('utf-8'),
            headers={**headers, "Content-Type": "application/json"},
            method="POST"
        )
        with urllib.request.urlopen(req) as res:
            print("Successfully created GitLab Release!")
            return True
    except urllib.error.HTTPError as e:
        err_msg = e.read().decode('utf-8')
        if e.code == 409:
            print(f"Release for tag '{tag_name}' already exists. Updating release links...")
            for link in links:
                try:
                    link_url = f"{release_url}/{tag_name}/assets/links"
                    req_link = urllib.request.Request(
                        link_url,
                        data=json.dumps(link).encode('utf-8'),
                        headers={**headers, "Content-Type": "application/json"},
                        method="POST"
                    )
                    with urllib.request.urlopen(req_link) as _:
                        print(f"Added link '{link['name']}' to existing release.")
                except urllib.error.HTTPError as e_link:
                    link_err = e_link.read().decode('utf-8')
                    if e_link.code == 409:
                        print(f"Link '{link['name']}' already exists on release.")
                    else:
                        print(f"Error adding link '{link['name']}': {link_err}", file=sys.stderr)
            return True
        else:
            print(f"Error creating release: {err_msg}", file=sys.stderr)
            return False
    except Exception as e:
        print(f"Network error creating release: {e}", file=sys.stderr)
        return False

uploaded_links = []
base_gitlab_url = api_url.split('/api/v4')[0]

for file_path in files_to_upload:
    upload_res = upload_file_with_retry(file_path)
    if upload_res:
        absolute_url = base_gitlab_url + upload_res['url']
        uploaded_links.append({
            "name": os.path.basename(file_path),
            "url": absolute_url
        })

if not uploaded_links:
    print("Error: No files were successfully uploaded to GitLab.", file=sys.stderr)
    sys.exit(1)

success = create_or_update_release(uploaded_links)
if not success:
    sys.exit(1)
print("GitLab Release publication complete.")
