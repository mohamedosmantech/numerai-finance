import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { resolve } from 'path';

export default defineConfig({
  plugins: [react()],
  base: '/widget/',
  build: {
    outDir: '../src/main/resources/static/widget',
    emptyOutDir: true,
    rollupOptions: {
      input: {
        index: resolve(__dirname, 'index.html'),
        'loan-calculator': resolve(__dirname, 'loan-calculator.html'),
        'compound-interest': resolve(__dirname, 'compound-interest.html'),
        'tax-estimator': resolve(__dirname, 'tax-estimator.html')
      }
    }
  }
});
