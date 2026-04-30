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
      },
      colors: {
        warm: {
          bg: '#faf6f1',
          card: '#ffffff',
          muted: '#f5f0ea',
          border: '#e8e0d6',
        },
        brand: {
          DEFAULT: '#ff6900',
          hover: '#ff8721',
          active: '#f59e2f',
          light: 'rgba(255, 105, 0, 0.12)',
          'light-2': 'rgba(255, 105, 0, 0.06)',
        }
      },
      boxShadow: {
        'warm-sm': '0 1px 3px rgba(0,0,0,0.04), 0 1px 2px rgba(0,0,0,0.03)',
        'warm': '0 4px 12px rgba(0,0,0,0.05), 0 1px 3px rgba(0,0,0,0.03)',
        'warm-md': '0 8px 24px rgba(0,0,0,0.06), 0 2px 6px rgba(0,0,0,0.03)',
        'warm-lg': '0 16px 40px rgba(0,0,0,0.07), 0 4px 12px rgba(0,0,0,0.04)',
        'brand-glow': '0 0 20px rgba(255, 105, 0, 0.15)',
      },
      animation: {
        'fade-in': 'fadeIn 0.6s ease-out forwards',
        'slide-up': 'slideUp 0.5s ease-out forwards',
        'scale-in': 'scaleIn 0.4s ease-out forwards',
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
        scaleIn: {
          '0%': { opacity: '0', transform: 'scale(0.95)' },
          '100%': { opacity: '1', transform: 'scale(1)' },
        },
      },
    },
  },
  plugins: [],
}
