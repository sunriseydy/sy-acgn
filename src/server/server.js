import express from "express";
import ViteExpress from "vite-express";

const app = express();

app.get("/", (_, res) => res.json("Hello from express!"));

ViteExpress.listen(app, 9390, () =>
    console.log(`
🚀 Server ready at: http://localhost:9390
⭐️ See sample requests: http://pris.ly/e/js/rest-express#3-using-the-rest-api`),
);