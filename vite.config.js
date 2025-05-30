/// <reference types="vitest" />
import { defineConfig } from 'vite';
import wasm from "vite-plugin-wasm";

export default defineConfig({
    plugins: [
	wasm()
    ],
    esbuild: {
	supported: {
	    'top-level-await': true
	},
    },
    base: ''
});
