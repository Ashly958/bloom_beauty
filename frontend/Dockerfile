# SPA React 18 + Vite (fase de implementación).
# Mientras se migra el prototipo, esta imagen sirve el prototipo HTML/CSS de la raíz del repositorio.
#
# Versión React (cuando exista package.json):
#   FROM node:20-alpine AS build
#   WORKDIR /app
#   COPY package*.json ./
#   RUN npm ci
#   COPY . .
#   RUN npm run build
#   FROM nginx:1.27-alpine
#   COPY --from=build /app/dist /usr/share/nginx/html
FROM nginx:1.27-alpine
COPY prototipo/ /usr/share/nginx/html/
EXPOSE 80
