import Vue from 'vue'
import VueRouter from 'vue-router'
import Home from '../views/Home.vue'
import Search from '../views/Search.vue'
import SearchResults from '../views/SearchResults.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/search',
    name: 'Search',
    component: Search
  },
  {
    path: '/results*',
    name: 'SearchResults',
    component: SearchResults
  },
  {
    path: '/:name*',
    name: 'Home',
    component: Home
  }
]

const router = new VueRouter({
  routes
})

export default router
