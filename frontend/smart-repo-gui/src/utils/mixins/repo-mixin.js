import {mapState} from 'vuex';

const repoMixin ={
    computed: {
        ...mapState('repo', {
            files:(state) => state.files,
            isUploaded:(state) => state.isUploaded,
            breadcrumbItems:(state) => state.breadcrumbItems,

        })
    }
}
export {repoMixin}