import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { SearchComponent } from './search/search.component';
import { ReactiveFormsModule } from '@angular/forms';
import { HomepageComponent } from './homepage/homepage.component';
import { SearchService } from 'src/services/search';

@NgModule({
  declarations: [
    AppComponent,
    SearchComponent,
    HomepageComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    ReactiveFormsModule
  ],
  providers: [SearchService],
  bootstrap: [AppComponent]
})
export class AppModule { }
