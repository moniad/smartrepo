<template>
  <v-breadcrumbs :items="$store.state.repo.breadcrumbItems">
    <template v-slot:item="{ item }">
      <v-breadcrumbs-item
          :disabled="item.disabled"
          @click="goTo(item.href)"
      >
        {{ item.text }}
      </v-breadcrumbs-item>
    </template>
  </v-breadcrumbs>
</template>

<script>
/* eslint-disable no-unused-vars */

import {repoMixin} from "../../utils/mixins/repo-mixin";
import {mapActions} from "vuex";
export default {
  name: "breadcrumb",
  data: () => ({
    name:'',
    from:'',
  }),
  props:{
    // trigger:0,
    // text:'',
    // href:'',
  },
  methods: {
    ...mapActions("repo",["loadFiles"]),
    ...mapActions("repo",["updateBreadcrumbItems"]),
    goTo (payload) {
      this.updateBreadcrumbItems(payload)
      this.$router.push(payload);
    }

  },
  watch: {
    $route(to, from) {
      console.log(to)
      this.updateBreadcrumbItems(to.path)
      if(to.path !== '/'){
        this.name = to.path
      } else this.name = ''
      this.loadFiles(to.path)
      this.from = from
    },
    'trigger'(){
      console.log(this.trigger)
    }
  }
}
</script>

<style scoped>

</style>