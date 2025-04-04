<template>
  <div id="app">
    <MainLayout />
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import MainLayout from "@/layouts/MainLayout.vue";

export default defineComponent({
  name: "App",
  components: {
    MainLayout,
  },
});
</script>

<style>
/* 全局页面背景和字体 */
body {
  margin: 0;
  font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
  background-color: #fff0f5;
}
</style>
