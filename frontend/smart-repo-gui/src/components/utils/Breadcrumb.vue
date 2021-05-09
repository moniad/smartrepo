<template>
  <v-breadcrumbs large :items="items"></v-breadcrumbs>
</template>

<script>
/* eslint-disable no-unused-vars */

export default {
  name: "Breadcrumb",
  data(){
    return {
      items: [
        {
          text: 'files',
          disabled: false,
          to: '#',
        },
      ],
    }
  },
  props:{
    // trigger:0,
    // text:'',
    // href:'',
  },
  methods: {
    isHome() {
      return this.items.length > 1;
    },
    addBreadcrumb(text, to) {
      this.items.push({text, to});
    },
    removeLastBreadcrumb() {
      this.isHome && this.items.pop();
    },
  },
  watch: {
    $route(to, from) {
      const {name, meta, /*  params, */ path} = to;
      console.log(name)
      console.log(meta)
      console.log(path)
    },
    'trigger'(){
      console.log(this.trigger)
    }
  }
}
</script>

<style scoped>

</style>