import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";
// import store from "./store"; // 如果你还没用到，可以先注释

createApp(App).use(router).mount("#app");
