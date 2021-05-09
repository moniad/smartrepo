/* eslint-disable no-unused-vars */
import axios from "axios";
import qs from "qs";

const defaultState = () => {
    return {
        files: [],
    };
};

const repoModule = {
    namespaced: true,
    state: () => {
        return {
            files: [],
            isUploaded: false
        };
    },
    mutations: {
        UPDATE_FILES(state, files){
            state.files = files;
        },
        FILES_UPLOADED(state){
            state.isUploaded = true;
            setTimeout(()=>{
                state.isUploaded = false;
            },3000)
        },
        RESTART_FILES(state){
            state.files = [];
            state.isUploaded=false;
        }
    },
    actions: {
        uploadFiles(
            { commit, dispatch, rootGetters, getters, rootState, state }, file
        ) {
            axios.post("http://localhost:7777/upload", file,
                {
                    headers: {
                        'Content-Type': 'multipart/form-data'
                    }
                }
            )
                .then(async response => {
                    if (response.status === 200) {
                        console.log("Response: " + response.data)
                        commit('FILES_UPLOADED')
                        dispatch('loadFiles');
                    } else {
                        console.log("ERROR: (" + response.status + ")")
                    }
                })
                .catch(error => {
                    console.error("An error occurred during receiving response!\n", error);
                })
        },
        loadFiles({ commit, dispatch, rootGetters, getters, rootState, state }, name){
            axios.get("http://localhost:7777/files", {params:{
                path:name?name:''
                }})
                .then(async response =>{
                    commit('UPDATE_FILES',response.data)
                })
                .catch(error =>{
                    console.error("An error occurred during receiving response!\n", error)
                })
        },
        resetFiles({ commit, dispatch, rootGetters, getters, rootState, state }){
            commit('RESTART_FILES');
        },
        fileDelete({ commit, dispatch, rootGetters, getters, rootState, state }, path){
            console.log(path)
            axios.delete("http://localhost:7777/files", {params:{
                path:path
                }})
                .then(async response =>{
                    console.log(response)
                    dispatch('loadFiles',path.split('/').slice(0, -1).join('/'));
                })
                .catch(error =>{
                    console.error("An error occurred during deleting file!\n", error)
                })
        },
        directoryPost({ commit, dispatch, rootGetters, getters, rootState, state }, path){
            console.log(path)
            axios.post("http://localhost:7777/files", {path:path})
                .then(async response =>{
                    console.log(response)
                    dispatch('loadFiles',path.split('/').slice(0, -1).join('/'));
                })
                .catch(error =>{
                    console.error("An error occurred during creating directory!\n", error)
                })
        },
        search({ commit, dispatch, rootGetters, getters, rootState, state }, { phrase, languages }){
            axios.get("http://localhost:7777/search", {params:{
                    phrase:phrase?phrase:'',
                    languages:languages?languages:[],
                },
                paramsSerializer: params => {
                    return qs.stringify(params, { arrayFormat: 'comma', encode: false })
                  }
                })
                .then(async response =>{
                    commit('UPDATE_FILES',response.data)
                })
                .catch(error =>{
                    console.error("An error occurred during receiving response!\n", error)
                })
        },
    },
    getters:{

    }
}
export default repoModule;