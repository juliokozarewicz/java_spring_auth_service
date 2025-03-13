FROM nginx:latest

# Install gettext and Nginx
RUN apt update && apt install -y gettext nginx

# Copy nginx configuration file
COPY nginx.conf.template /etc/nginx/nginx.conf.template

# Copy entrypoint script and make it executable
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# Set the entrypoint script to run when the container starts
ENTRYPOINT ["/entrypoint.sh"]