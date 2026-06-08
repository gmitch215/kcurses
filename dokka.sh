git config --local user.email "action@github.com"
git config --local user.name "GitHub Action"

if [ ! -d "docs" ]; then
  mkdir docs
fi

cp -Rfv build/dokka/html/* ./docs/

if git ls-remote --exit-code --heads origin gh-pages >/dev/null 2>&1; then
  git fetch origin gh-pages
  git switch -f --track origin/gh-pages
else
  echo "gh-pages branch does not exist, creating it"

  git switch --orphan gh-pages
  git rm -rf . >/dev/null 2>&1 || true
fi

for dir in ./*; do
  if [ "$dir" = "./docs" ]; then
    continue
  fi

  rm -rf "$dir"
done

cp -Rfv ./docs/* ./
rm -rf ./docs

git add .

if git diff --cached --quiet; then
  echo "No changes to deploy"
else
  git commit -m "Update Dokka ($1)"
  git push -f origin gh-pages
fi