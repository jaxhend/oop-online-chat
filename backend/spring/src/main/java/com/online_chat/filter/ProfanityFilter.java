package com.online_chat.filter;

import jakarta.annotation.PostConstruct;
import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class ProfanityFilter {
    // Online chat kasutab roppuste välja filtreerimiseks Aho-CoraSick algoritmi.
    private static final Logger logger = LoggerFactory.getLogger(ProfanityFilter.class);
    private Trie trie;

    @PostConstruct
    public void init() {
        List<String> words = loadProfanity();
        buildTrie(words);
    }

    private List<String> loadProfanity() {
        List<String> words = new ArrayList<>();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("profanity.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null)
                words.add(line.trim());
        } catch (IOException e) {
            logger.error("ProfanityFilter viskas faili lugemisel IOExecptioni:  ", e);
        }
        return words;
    }

    private void buildTrie(List<String> words) {
        Trie.TrieBuilder builder = Trie.builder()
                .ignoreCase()
                .ignoreOverlaps();
        words.forEach(builder::addKeyword);
        trie = builder.build();
    }

    public boolean containsProfanity(String message) {
        if (message == null || message.isEmpty()) return false;

        Collection<Emit> emits = trie.parseText(message);
        return !emits.isEmpty();
    }

    public String filterMessage(String message) {
        if (message == null || message.isEmpty()) return message;

        Collection<Emit> emits = trie.parseText(message);
        if (emits.isEmpty()) return message;

        char[] chars = message.toCharArray();
        for (Emit emit : emits) {
            for (int i = emit.getStart(); i <= emit.getEnd(); i++)
                chars[i] = '*';
        }
        return new String(chars);
    }

}
