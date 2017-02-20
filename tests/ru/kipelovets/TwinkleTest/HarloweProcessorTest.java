package ru.kipelovets.TwinkleTest;

import org.attoparser.ParseException;
import org.junit.Assert;
import org.junit.Test;
import ru.kipelovets.Twinkle.HarloweProcessor;
import ru.kipelovets.Twinkle.Main;
import ru.kipelovets.Twinkle.Novel.Novel;
import ru.kipelovets.Twinkle.Novel.Stitch;

import java.io.IOException;

public class HarloweProcessorTest  {

    @Test
    public void testStitchConditions() throws IOException, ParseException {
        String document = Main.read("./tests/StitchConditions.html");
        Novel novel = (new HarloweProcessor(document)).getNovel();
        Assert.assertNotNull(novel);
        Assert.assertEquals(1, novel.getStitches().size());
        Stitch stitch = novel.getStitches().get(0);
        Assert.assertEquals(1, (int)stitch.getIfConditions().size());
        Assert.assertEquals("trade < 2", stitch.getIfConditions().get(0));
        Assert.assertEquals("Торговаться ты не умеешь", stitch.getText());
        Assert.assertEquals(1, stitch.getOptions().size());
        Assert.assertEquals("Поезжай на вокзал. Там иди в камеру хранения, ящик 543. Оттуда напиши",
                stitch.getOptions().get(0).getOption());
    }

    @Test
    public void testInlineConditions() throws IOException, ParseException {
        String document = Main.read("./tests/InlineCondition.html");
        Novel novel = (new HarloweProcessor(document)).getNovel();
        Assert.assertNotNull(novel);
        Assert.assertEquals(1, novel.getStitches().size());
        Stitch stitch = novel.getStitches().get(0);
        Assert.assertEquals("Я на месте, {trade > 1:братишка|командир}", stitch.getText());
        Assert.assertEquals(1, stitch.getOptions().size());
        Assert.assertEquals("Вводи код: Код 568203",
                stitch.getOptions().get(0).getOption());
    }

    @Test
    public void testVariables() throws IOException, ParseException {
        String document = Main.read("./tests/VariableValue.html");
        Novel novel = (new HarloweProcessor(document)).getNovel();
        Assert.assertNotNull(novel);
        Assert.assertEquals(1, novel.getStitches().size());
        Stitch stitch = novel.getStitches().get(0);
        Assert.assertEquals("[value:ping] Я знаю, что ты припрятал кое-что. Мы можем найти это вместе.", stitch.getText());
        Assert.assertEquals(0, stitch.getOptions().size());
        Assert.assertEquals("9",
                stitch.getDivert());
    }


}
