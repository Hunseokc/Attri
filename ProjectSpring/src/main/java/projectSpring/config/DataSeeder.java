package projectSpring.config;

import projectSpring.entity.Feed;
import projectSpring.entity.Tag;
import projectSpring.repository.FeedRepository;
import projectSpring.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final FeedRepository feedRepository;
    private final TagRepository tagRepository;

    @Override
    public void run(String... args) throws Exception {
        if (feedRepository.count() == 0) {

            // DB에 있으면 꺼내오고 없으면 저장
            Tag mixing = tagRepository.findByName("Mixing")
                    .orElseGet(() -> tagRepository.save(new Tag("Mixing")));
            Tag illustration = tagRepository.findByName("Illustration")
                    .orElseGet(() -> tagRepository.save(new Tag("Illustration")));
            Tag beat = tagRepository.findByName("Beat")
                    .orElseGet(() -> tagRepository.save(new Tag("Beat")));

            // 2. 피드 생성
            Feed feed1 = new Feed();
            feed1.setTitle("Rs124-style Compression Test");
            feed1.setCreator("MixMaster");
            feed1.setType("video");
            feed1.setHeight(250);
            feed1.setCollab(false);
            feed1.getTags().add(mixing);

            Feed feed2 = new Feed();
            feed2.setTitle("60s Psychedelic Pop Cover Draft");
            feed2.setCreator("ArtStudio");
            feed2.setType("art");
            feed2.setHeight(350);
            feed2.setCollab(true);
            feed2.getTags().add(illustration);

            Feed feed3 = new Feed();
            feed3.setTitle("Vintage Drum Loops");
            feed3.setCreator("BeatMaker");
            feed3.setType("music");
            feed3.setHeight(320);
            feed3.setCollab(true);
            feed3.getTags().add(beat);

            // 3. 피드 DB에 저장
            feedRepository.save(feed1);
            feedRepository.save(feed2);
            feedRepository.save(feed3);

            System.out.println("초기 테스트 데이터가 DB에 성공적으로 저장되었습니다!");
        }
    }
}