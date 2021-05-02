<template>
  <v-sheet
      height="100%"
      class="overflow-hidden"
      style="position: relative;">
    <v-container :fluid="true" class>
      <breadcrumb :trigger="breadcrumbTrigger" :text="breadcrumbText" :href="breadcrumbHref"/>
      <v-data-table :headers="headers"
                    :items="items"
                    :search="search"
                    hide-default-footer>
        <template v-slot:top>
          <v-text-field
              v-model="search"
              label="Search"
              class="mx-4"
          ></v-text-field>
        </template>
        <template #item="{ item }">
          <tr @click="to(item.name,item.directory)">
            <td class="pr-0">
              <v-layout justify-end>
                <v-icon v-if="item.directory">mdi-folder</v-icon>
                <v-icon v-else-if="item.type === 'png'">mdi-image-outline</v-icon>
                <v-icon v-else>mdi-file-outline</v-icon>
              </v-layout>
            </td>
            <td> {{item.name}}</td>
            <td> {{parseDate(item.createDate)}}</td>
            <td> {{item.extension}}</td>
            <td> {{item.size}}</td>
            <td class="pl-0">
              <v-btn class="ma-2" icon @click.stop="drawerItem = item;drawerActive = !drawerActive">
                <v-icon>mdi-information-outline</v-icon>
              </v-btn>
            </td>
          </tr>
        </template>
      </v-data-table>
<!--      <v-navigation-drawer-->
<!--          v-model="drawerActive"-->
<!--          v-if="drawerActive"-->
<!--          absolute-->
<!--          right-->
<!--          temporary-->
<!--          width="25%"-->
<!--      >-->
<!--        <v-list subheader>-->
<!--          <v-subheader>Title</v-subheader>-->
<!--          <v-list-item>-->
<!--            <v-list-item-content>-->
<!--              <v-list-item-title>{{ drawerItem.name }}</v-list-item-title>-->
<!--            </v-list-item-content>-->
<!--          </v-list-item>-->
<!--        </v-list>-->

<!--        <v-divider></v-divider>-->
<!--        <v-img-->
<!--            v-if="drawerItem.type === 'png'"-->
<!--            src="https://cdn.vuetifyjs.com/images/lists/ali.png"-->
<!--            height="300px"-->
<!--            dark-->
<!--        />-->
<!--        <v-divider/>-->
<!--        <v-list subheader>-->
<!--          <v-subheader>Details</v-subheader>-->
<!--          <v-list-item>-->
<!--            Uploaded date: {{drawerItem.upload_date}}-->
<!--          </v-list-item>-->
<!--          <v-list-item>-->
<!--            Uploaded by: {{drawerItem.uploaded_by}}-->
<!--          </v-list-item>-->
<!--          <v-list-item>-->
<!--            Type: {{drawerItem.type}}-->
<!--          </v-list-item>-->
<!--          <v-list-item>-->
<!--            Size: {{drawerItem.size}}-->
<!--          </v-list-item>-->
<!--        </v-list>-->
<!--        <v-divider/>-->
<!--        <v-list two-line subheader>-->
<!--          <v-subheader>Activity</v-subheader>-->
<!--          <v-list-item>-->
<!--            <v-list-item-content>-->
<!--              <v-list-item-title>-->
<!--                Uploaded by: {{drawerItem.uploaded_by}}-->
<!--              </v-list-item-title>-->
<!--              <v-list-item-subtitle-->
<!--                  class="text&#45;&#45;primary"-->
<!--                  v-text="drawerItem.upload_date"-->
<!--              >-->
<!--              </v-list-item-subtitle>-->
<!--            </v-list-item-content>-->
<!--          </v-list-item>-->
<!--        </v-list>-->
<!--      </v-navigation-drawer/>-->
    </v-container>
  </v-sheet>
</template>
<script>
import {mapActions} from "vuex";
import {repoMixin} from "../utils/mixins/repo-mixin";
import {dataMixin} from "../utils/mixins/handle-data";
export default {
  name: "home",
  data: () => ({
    search:'',
    drawerItem: null,
    drawerActive: false,
    name:'',
    breadcrumbTrigger:0,
    breadcrumbText: '',
    breadcrumbHref:''
  }),
  components:{

  },
  mixins: [repoMixin, dataMixin],
  computed: {
    headers() {
      return [
        {text: '', value: 'icon', align: 'end'},
        {text: 'Name', value: 'name'},
        {text: 'Upload Date', value: 'createDate'},
        {text: 'Type', value: 'extension'},
        {text: 'Size', value: 'size'},
        {text: 'Info', value: 'info'}
      ];
    },
    items() {
      return this.files
    },
  },
  methods: {
    ...mapActions("repo",["loadFiles"]),
    to(name,directory) {
      if(directory){
        this.name = this.name +'/'+name
        this.$router.push({
          path: this.name
        })
      }
    },
  },
  watch:{
    $route(to,from){
      if(to.path !== '/'){
        this.name = to.path
      } else this.name = ''
      this.loadFiles(to.path)
      this.from=from
    },
    items() {
      return this.files
    },
  },
  created() {
    this.loadFiles(this.$route.path)
  }
};
</script>