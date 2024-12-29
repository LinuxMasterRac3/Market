s
#!/bin/bash

cd /home/pado/Documents/code/GestionaleJavawebUI0.21 || exit 1

# Arresta e rimuove i container esistenti
sudo docker-compose down

# Ricostruisce le immagini e avvia i container in modalità detached
sudo docker-compose up --build -d

echo "Docker containers ricostruiti e avviati."