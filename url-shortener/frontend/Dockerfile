# --- Stage 1: Build ---
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# --- Stage 2: Production ---
FROM nginx:alpine
# Copy the built files from Vite's 'dist' folder to Nginx
COPY --from=build /app/dist /usr/share/nginx/html
# Copy a custom nginx config if you have one, otherwise default is fine for a SPA
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]