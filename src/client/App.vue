<template>
  <v-app id="inspire">
    <app-navigation-drawer />
    <v-main>
      <v-container
        fluid
        class="pa-0"
      >
        <router-view />
      </v-container>
    </v-main>
  </v-app>
</template>
<script setup>
import AppNavigationDrawer from '@/client/components/AppNavigationDrawer.vue'
</script>
