import Vue from 'vue'
import Vuex from 'vuex'
import repoModule from "./modules/repo";

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
  },
  mutations: {
  },
  actions: {
  },
  modules: {
    repo: repoModule
  }

})
