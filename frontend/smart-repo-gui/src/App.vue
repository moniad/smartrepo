<template>
  <v-app>
    <nav-bar/>
    <v-main>
      <router-view></router-view>
    </v-main>
  </v-app>
</template>

<script>
import NavBar from "@/components/navigation/NavBar";

export default {
  name: 'App',

  components: {
    NavBar
  },

  data: () => ({
    //
  }),
};
</script>
<style lang="scss">
@import "./styles/app.scss";
</style>

