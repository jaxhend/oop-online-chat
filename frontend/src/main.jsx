import React from "react";
import ReactDOM from "react-dom/client";
import OnlineChat from "./OnlineChat";
import {CookiesProvider} from "react-cookie";
import "./index.css";

ReactDOM.createRoot(document.getElementById("root")).render(
    <React.StrictMode>
        <CookiesProvider>
            <OnlineChat />
        </CookiesProvider>
    </React.StrictMode>
);