import { defineConfig } from 'vite';
import { viteSingleFile } from 'vite-plugin-singlefile';
import path from 'path';

export default defineConfig({
  plugins: [viteSingleFile()],
  build: {
    outDir: path.resolve(__dirname, '../app/src/main/assets/web'),
    emptyOutDir: false,
    target: 'es2020',
    cssMinify: true,
    minify: true
  }
});
