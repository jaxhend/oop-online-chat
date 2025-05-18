import React from "react";
import ReactDOM from "react-dom/client";
import OnlineChat from "./OnlineChat";
import {CookiesProvider} from "react-cookie";
import {ThemeProvider} from './components/context/ThemeContext';
import "./index.css";

ReactDOM.createRoot(document.getElementById("root")).render(
    <CookiesProvider>
        <ThemeProvider>
            <OnlineChat/>
        </ThemeProvider>
    </CookiesProvider>
);
