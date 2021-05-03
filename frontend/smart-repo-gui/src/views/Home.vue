<template>
  <v-sheet height="100%" class="overflow-hidden" style="position: relative">
    <v-container :fluid="true" class>
      <breadcrumb
        :trigger="breadcrumbTrigger"
        :text="breadcrumbText"
        :href="breadcrumbHref"
      />
      <v-app-bar>
        <button
          type="button"
          @click="showDirectoryInput()"
          title="Create directory"
        >
          <v-icon>mdi-plus</v-icon>
        </button>
        <v-text-field
          v-model="directoryName"
          label="Directory name"
          class="mx-4"
          v-if="directoryInputVisible"
        />
        <v-btn v-if="directoryInputVisible" @click="createDirectory()">Create</v-btn>
      </v-app-bar>
      <v-data-table
        :headers="headers"
        :items="items"
        :search="search"
        hide-default-footer
      >
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
              <v-btn
                class="ma-2"
                icon
                @click.stop="
                  drawerItem = item;
                  drawerActive = !drawerActive;
                "
              >
                <v-icon>mdi-information-outline</v-icon>
              </v-btn>
            </td>
            <td>
              <button
                type="button"
                @click="deleteFile(item.name)"
                title="Remove file"
              >
                <v-icon>mdi-delete</v-icon>
              </button>
            </td>
          </tr>
        </template>
      </v-data-table>
      <v-snackbar
          v-model="isUploaded"
      >
        Files added successfully

        <template v-slot:action="{ attrs }">
          <v-btn
              color="green"
              text
              v-bind="attrs"
              @click="isUploaded = false"
          >
            Close
          </v-btn>
        </template>
      </v-snackbar>
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
    search: "",
    drawerItem: null,
    drawerActive: false,
    name:'',
    breadcrumbTrigger:0,
    breadcrumbText: '',
    breadcrumbHref:'',
    directoryInputVisible: false,
    directoryName: "",
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
    ...mapActions("repo", ["fileDelete"]),
    ...mapActions("repo", ["directoryPost"]),
    to(name,directory) {
      if(directory){
        this.name = this.name +'/'+name
        this.$router.push({
          path: this.name,
        });
      }
    },
    deleteFile(name) {
      //TODO: pass directory (that info should be stored when file tree is displayed properly)
      let currentDirectory = ''
      name = 'test.txt'
      let path = currentDirectory+name;
      this.fileDelete(path);
    },
    showDirectoryInput() {
      this.directoryInputVisible = true;
    },
    createDirectory(){
      //TODO: pass directory (that info should be stored when file tree is displayed properly)
      let currentDirectory = ''
      let name = this.directoryName;
      let path = currentDirectory+name;
      console.log(path)
      this.directoryPost(path);
      this.directoryName = "";
      //TODO: reload view with files
    }
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
  beforeMount() {
    this.loadFiles(this.$route.path);
  },
};
</script>