STATIC=public/*.html public/*.css public/img public/template public/icon.* public/fonts.json # public/images public/assets

all: build build/server.js

build/server.js: src/**/*.cljs shadow-cljs.edn node_modules
	npx shadow-cljs release server --debug

public/fonts.json:
	wget https://static.itch.io/fonts.json -O "$@"

build: src/**/* $(STATIC)
	mkdir -p build/public
	cp -LR --preserve=all $(STATIC) build/public
	npx shadow-cljs release app
	touch build

node_modules: package.json
	pnpm i --no-lockfile --shamefully-hoist
	touch node_modules

.PHONY: watch watcher server repl clean

server: node_modules
	@echo "waiting for devserver.js to appear."
	@rm -f devserver.js; until [ -f devserver.js -a -d .shadow-cljs ]; do sleep 1; done; echo "devserver.js appeared. starting."
	@sleep 1 && while [ 1 ]; do DEV=1 node devserver.js; sleep 3; echo "restarting devserver.js"; done

watcher:
	npx shadow-cljs watch server app

watch:
	make -j2 watcher server

repl:
	npx shadow-cljs cljs-repl app

clean:
	rm -rf .shadow-cljs devserver.js server.js build node_modules package-lock.json
