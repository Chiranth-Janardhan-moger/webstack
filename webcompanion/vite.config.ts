import { defineConfig, type Plugin } from 'vite';
import { viteSingleFile } from 'vite-plugin-singlefile';
import path from 'path';

import fs from 'fs';

function postCleanSingleFile(): Plugin {
  return {
    name: 'post-clean-singlefile',
    closeBundle() {
      const outHtml = path.resolve(__dirname, '../app/src/main/assets/web/index.html');
      if (fs.existsSync(outHtml)) {
        let content = fs.readFileSync(outHtml, 'utf-8');
        const scriptMatch = content.match(/<script\b[^>]*>[\s\S]*?<\/script>/i);
        if (scriptMatch) {
          let scriptTag = scriptMatch[0];
          scriptTag = scriptTag.replace(/^<script\b[^>]*>/i, '<script>');

          content = content.replace(scriptMatch[0], '');
          content = content.replace(/<link\s+rel="modulepreload"[^>]*>/gi, '');
          content = content.replace('</body>', scriptTag + '\n</body>');
        }
        fs.writeFileSync(outHtml, content, 'utf-8');
      }
    }
  };
}


export default defineConfig({
  plugins: [viteSingleFile(), postCleanSingleFile()],
  build: {
    outDir: path.resolve(__dirname, '../app/src/main/assets/web'),
    emptyOutDir: false,
    target: 'es2020',
    cssMinify: true,
    minify: true
  }
});

