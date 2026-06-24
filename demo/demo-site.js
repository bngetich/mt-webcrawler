const http = require("http");

const PORT = 8081;

const pages = {
  "/": `
    <!doctype html>
    <html>
      <head><title>Crawler Demo Home</title></head>
      <body>
        <h1>Crawler Demo Site</h1>
        <p>This page links to allowed and disallowed crawler targets.</p>
        <a href="/page-1">Page 1</a>
        <a href="/page-2">Page 2</a>
        <a href="/private/secret">Private Secret</a>
      </body>
    </html>
  `,
  "/page-1": `
    <!doctype html>
    <html>
      <head><title>Page 1</title></head>
      <body>
        <h1>Page 1</h1>
        <a href="/">Home</a>
        <a href="/page-2">Page 2</a>
      </body>
    </html>
  `,
  "/page-2": `
    <!doctype html>
    <html>
      <head><title>Page 2</title></head>
      <body>
        <h1>Page 2</h1>
        <a href="/">Home</a>
        <a href="/private/secret">Private Secret</a>
      </body>
    </html>
  `,
  "/private/secret": `
    <!doctype html>
    <html>
      <head><title>Private Secret</title></head>
      <body>
        <h1>Private Secret</h1>
        <p>The crawler should skip this page because robots.txt disallows /private.</p>
      </body>
    </html>
  `,
};

const robotsTxt = `User-agent: *
Disallow: /private
Crawl-delay: 2
`;

const server = http.createServer((req, res) => {
  const url = new URL(req.url, `http://${req.headers.host}`);

  if (url.pathname === "/robots.txt") {
    res.writeHead(200, { "Content-Type": "text/plain" });
    res.end(robotsTxt);
    return;
  }

  const page = pages[url.pathname];

  if (!page) {
    res.writeHead(404, { "Content-Type": "text/plain" });
    res.end("Not found");
    return;
  }

  res.writeHead(200, { "Content-Type": "text/html" });
  res.end(page);
});

server.listen(PORT, () => {
  console.log(`Demo site running at http://localhost:${PORT}/`);
  console.log(`robots.txt available at http://localhost:${PORT}/robots.txt`);
});
