<script setup lang="ts">
import AppNavigationDrawer from '@client/components/AppNavigationDrawer.vue'
import { VSonner } from 'vuetify-sonner'
</script>

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
    <VSonner position="bottom-right" />
  </v-app>
</template>
