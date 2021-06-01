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
            <td> {{ item.name }}</td>
            <td> {{ item.creationDate }}</td>
            <td> {{ item.modificationDate }}</td>
            <td> {{ item.extension }}</td>
            <td> {{ item.size }}</td>
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
                @click.stop="deleteFile(item.name)"
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
              @click="$store.state.repo.isUploaded=false"
          >
            Close
          </v-btn>
        </template>
      </v-snackbar>
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
        {text: 'Creation Date', value: 'creationDate'},
        {text: 'Modification Date', value: 'modificationDate'},
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
      let path = this.name+'/'+name;
      this.fileDelete(path);
    },
    showDirectoryInput() {
      this.directoryInputVisible = true;
    },
    createDirectory(){
      let name = this.directoryName;
      let path = this.name+ '/'+name;
      this.directoryPost(path);
      this.directoryName = "";
    }
  },
  watch:{
    $route(to,from){
      if(to.path !== '/'){
        this.name = to.path
      } else this.name = ''
      this.loadFiles(to.path)
      this.from = from
    },
    items() {
      return this.files
    },
  },
  beforeMount() {
    this.name = this.$route.path === '/'? this.name:this.$route.path
    this.loadFiles(this.$route.path);
  },
};
</script>