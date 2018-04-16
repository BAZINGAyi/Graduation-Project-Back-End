package com.potflesh.wenda.service;
import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bazinga on 2017/4/13.
 */
@Service
public class SensitiveService implements InitializingBean{

    private static final Logger logger= LoggerFactory.getLogger(SensitiveService.class);

    private static final String DEFAULT_REPLACEMENT = "*敏感词*";

    @Override
    public void afterPropertiesSet() throws Exception {
        try{
            InputStream is = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream("SensitiveWords.txt");
            InputStreamReader read = new InputStreamReader(is);
            BufferedReader bufferedReader = new BufferedReader(read);
            String lineText;
            while ((lineText = bufferedReader.readLine())!=null){
                addWord(lineText.trim());
            }

            read.close();

        }catch (Exception e){
            logger.error("读取敏感词出错"+e.getMessage());
        }

    }
    // 增加敏感词
    private void addWord(String lineText){

        TrieNode trieNode = rootNode;

        for(int i = 0; i < lineText.length(); ++i){

            Character c = lineText.charAt(i);

            if(isSymbol(c))
                continue;

           // System.out.println("字符：" + c);

            TrieNode node = trieNode.getSubNode(c);

            // 该分支没有结点，把新的结点加入
            if(node == null){
                node = new TrieNode();
                trieNode.addSubNode(c,node);
            }

            trieNode = node;

            if(i == lineText.length()-1)
                trieNode.setKeyWord(true);
        }
    }

    // 过滤
    public String filter(String text){
        if(StringUtils.isBlank(text))
            return text;

        StringBuilder result = new StringBuilder();

        TrieNode tempNode = rootNode;

        int begin = 0;

        int position = 0;

        while (position < text.length()){

            char c = text.charAt(position);

            // 如果是非法词
            if(isSymbol(c)){
                // 如果开头是非法词，则放入 result
                if(tempNode == rootNode){
                    result.append(c);
                    ++begin;
                }
                position++;
                continue;
            }

            tempNode = tempNode.getSubNode(c);

            // 没有敏感词
            if(tempNode == null){
                result.append(text.charAt(begin));
                position = begin + 1;
                begin = position;
                tempNode = rootNode;
            }else if(tempNode.isKeyWordEnd()){
                // 发现敏感词
                result.append(DEFAULT_REPLACEMENT);
                position = position + 1;
                begin = position;
                tempNode = rootNode;
            }else {
                ++position;
            }
        }

        // 假如最后几个位置属于字典树，但没有做标记，所以加到 result 中。
        result.append(text.substring(begin));

        return result.toString();
    }

    private boolean isSymbol (char c){
        int ic = (int)c;
        // 如果不是 ascill 码的数字 东亚文字 0x2E80 - 0x9fff
        return !CharUtils.isAsciiAlphanumeric(c) && (ic < 0x2E80 || ic > 0x9fff);
    }

    // 前缀树结点
    private class TrieNode{
        // 关键词的结尾
        private boolean end = false;
        // 当前结点下所有的子结点
        private Map<Character,TrieNode> subNodes = new HashMap<Character,TrieNode>();

        private void addSubNode(Character key,TrieNode node){
            subNodes.put(key,node);
        }

        private TrieNode getSubNode(Character key){
            return subNodes.get(key);
        }

        boolean isKeyWordEnd(){
            return end;
        }

        void setKeyWord(Boolean end){
            this.end = end;
        }

    }

    private  TrieNode rootNode = new TrieNode();

}
