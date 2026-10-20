/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      fontFamily: {
        display: ['"Sora"', 'system-ui', 'sans-serif'],
        body: ['"Outfit"', 'system-ui', 'sans-serif'],
        mono: ['"JetBrains Mono"', 'monospace'],
      },
      colors: {
        brand: {
          50: '#eef9ff',
          100: '#d9f1ff',
          200: '#bce7ff',
          300: '#8ed8ff',
          400: '#59c0ff',
          500: '#33a2ff',
          600: '#1c82f5',
          700: '#1668e1',
          800: '#1854b6',
          900: '#1a498f',
        },
        ink: {
          900: '#0a0e1a',
          800: '#111726',
          700: '#1a2235',
          600: '#27324a',
        },
      },
      boxShadow: {
        glow: '0 0 40px -10px rgba(51,162,255,0.45)',
        card: '0 10px 40px -12px rgba(0,0,0,0.25)',
      },
      backgroundImage: {
        'mesh': 'radial-gradient(at 20% 20%, rgba(51,162,255,0.18) 0px, transparent 50%), radial-gradient(at 80% 0%, rgba(124,92,255,0.16) 0px, transparent 50%), radial-gradient(at 60% 90%, rgba(0,209,178,0.12) 0px, transparent 50%)',
      },
      keyframes: {
        floaty: {
          '0%,100%': { transform: 'translateY(0px)' },
          '50%': { transform: 'translateY(-12px)' },
        },
        shimmer: {
          '100%': { transform: 'translateX(100%)' },
        },
      },
      animation: {
        floaty: 'floaty 6s ease-in-out infinite',
        shimmer: 'shimmer 1.6s infinite',
      },
    },
  },
  plugins: [],
};
