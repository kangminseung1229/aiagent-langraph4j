package ai.langgraph4j.aiagent.parser;

import java.util.List;

import lombok.Data;

@Data
public class ParserResultLaw {

        private String orig_txt;
        private String full_txt;
        private String law_db;
        private String law_id;
        private String law_sno;
        private String law_type;
        private String my_type;
        private String imply_dy;
        private String base_dy;
        private String group_no;
        private String article;
        private String paragraph;
        private String subparagraph;
        private String item;
        private String attached_table;
        private String attached_form;
        private String law_api_link;

        private List<Integer> loc;
        private List<Integer> article_range;
        private List<Integer> paragraph_range;
        private List<Integer> subparagraph_range;
        private List<String> item_range;
}
