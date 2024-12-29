# 1. Deinizializza e rimuovi i submodules
git submodule deinit -f web
git submodule deinit -f comandi
git rm --cached web
git rm --cached comandi
rm -rf .git/modules/web
rm -rf .git/modules/comandi

# 2. Rimuovi .gitmodules
rm .gitmodules

# 3. Backup dei contenuti
mv web web_backup
mv comandi comandi_backup

# 4. Ricrea le cartelle come normali directory
mkdir web
mkdir comandi
cp -r web_backup/* web/
cp -r comandi_backup/* comandi/

# 5. Rimuovi i backup
rm -rf web_backup
rm -rf comandi_backup

# 6. Aggiungi le cartelle al repository principale
git add web/
git add comandi/
git commit -m "Converti submodules in cartelle normali"
git push origin main --force