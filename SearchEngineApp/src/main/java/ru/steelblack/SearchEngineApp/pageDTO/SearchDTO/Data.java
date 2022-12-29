package ru.steelblack.SearchEngineApp.pageDTO.SearchDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Data implements Comparable<Data> {

   private String site;

   private String siteName;

   private String url;

   private String title;

   private String snippet;

   private float relevance;

   @Override
   public String toString() {
      return  "url='" + url + ", title='" + title + ", snippet='" + snippet + ", relevance=" + relevance;
   }


   @Override
   public int compareTo(Data o) {
      int compare = 0;
      if ((this.getRelevance() - o.getRelevance()) > 0.0f){
         compare = -1;
      }
      if ((this.getRelevance() - o.getRelevance()) < 0.0f){
         compare = 1;
      }
      if ((this.getRelevance() - o.getRelevance()) == 0.0f){
         compare = 0;
      }
      return compare;
   }
}
