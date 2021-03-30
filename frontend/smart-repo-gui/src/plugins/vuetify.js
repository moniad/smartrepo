import Vue from "vue";
import Vuetify from "vuetify";
import "vuetify/dist/vuetify.min.css";
import "@mdi/font/css/materialdesignicons.css";

Vue.use(Vuetify);
export default new Vuetify({
    icons: {
        iconfont: "mdi", // 'mdi' || 'mdiSvg' || 'md' || 'fa' || 'fa4' || 'faSvg'
    },
    theme: {
        themes: {
            light: {
                primary: '#000000',
                secondary: '#ffffff',
                background: '#f9f9f9',
                dark : '#000000',
                light: '#ffffff',
                progress: '#000000',
            },
        },
    },
});
