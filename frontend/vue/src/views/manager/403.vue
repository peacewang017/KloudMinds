<template>
  <div>
    <div style="height: calc(100vh - 80px); overflow: hidden; display: flex; align-items: center; justify-content: center">
      <div style="font-size: 40px">Unauthorized access <router-link to="/">GO back to homepage</router-link></div>
    </div>
  </div>
</template>

<script>
export default {
  name: "Auth",
  data() {
    return {}
  },
  created() {

  },
  methods: {}
}
</script>

<style scoped>

</style>