import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

export default defineConfig({
    plugins: [react()],
    resolve: {
        alias: {
            '@': path.resolve(__dirname, './src'),
        },
    },
    server: {
        proxy: {
            '/ws': {
                target: 'ws://localhost:8080',
                ws: true,
            },
            '/ilm': 'http://localhost:8080',
            '/paevapakkumised': 'http://localhost:8080',
            '/uudised': 'http://localhost:8080',
            '/chatbot': 'http://localhost:8080',
            '/flask': 'http://localhost:5001',
        },
    },
});