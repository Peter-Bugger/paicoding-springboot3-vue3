/** @type {import('tailwindcss').Config} */
export default {
  content: [
    './index.html',
    './src/**/*.{vue,js,ts,jsx,tsx}',
  ],
  darkMode: 'class',
  theme: {
    extend: {
      width: {
        navBarMDInput: '300px',
        navBarInput: '200px',
      },
      fontFamily: {
        'serif-cn': ['"Noto Serif SC"', 'serif'],
        'mono-cn': ['"JetBrains Mono"', '"Source Code Pro"', 'monospace'],
        'display-cn': ['"ZCOOL QingKe HuangYou"', 'cursive'],
      },
      colors: {
        cool: {
          bg: '#f4f6fa',
          card: '#ffffff',
          muted: '#eef1f7',
          border: '#d6dae6',
          dark: '#1a1d27',
        },
        brand: {
          DEFAULT: '#2d7cf6',
          hover: '#4a8ff7',
          active: '#1a5fc7',
          light: 'rgba(45, 124, 246, 0.12)',
          'light-2': 'rgba(45, 124, 246, 0.06)',
          orange: '#ff6900',
        }
      },
      boxShadow: {
        'cool-sm': '0 1px 3px rgba(26,29,39,0.04), 0 1px 2px rgba(26,29,39,0.03)',
        'cool': '0 4px 12px rgba(26,29,39,0.05), 0 1px 3px rgba(26,29,39,0.03)',
        'cool-md': '0 8px 24px rgba(26,29,39,0.06), 0 2px 6px rgba(26,29,39,0.03)',
        'cool-lg': '0 16px 40px rgba(26,29,39,0.07), 0 4px 12px rgba(26,29,39,0.04)',
        'brand-glow': '0 0 20px rgba(45, 124, 246, 0.15)',
      },
      animation: {
        'fade-in': 'fadeIn 0.6s ease-out forwards',
        'slide-up': 'slideUp 0.5s ease-out forwards',
        'scale-in': 'scaleIn 0.4s ease-out forwards',
        'slide-down': 'slideDown 0.3s ease-out forwards',
        'shimmer': 'shimmer 2s infinite linear',
      },
      keyframes: {
        fadeIn: {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        slideUp: {
          '0%': { opacity: '0', transform: 'translateY(20px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        slideDown: {
          '0%': { opacity: '0', transform: 'translateY(-10px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        scaleIn: {
          '0%': { opacity: '0', transform: 'scale(0.95)' },
          '100%': { opacity: '1', transform: 'scale(1)' },
        },
        shimmer: {
          '0%': { backgroundPosition: '-200% 0' },
          '100%': { backgroundPosition: '200% 0' },
        },
      },
    },
  },
  plugins: [],
}
