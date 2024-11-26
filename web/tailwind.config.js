/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    extend: {
      colors: {
        primary: '#4CAF50',
        secondary: '#FF5722',
        dark: '#2C3E50',
        light: '#ECF0F1',
      },
    },
  },
  plugins: [],
  darkMode: 'class',
}

