idea-test:
	java -cp "out/production/TwineToInke/:lib/attoparser-2.0.3.RELEASE.jar:lib/javax.json.jar" ru.kipelovets.Twinkle.Main "Test story.html"

gradle-build:
	docker-compose run --rm gradle build

run:
	docker-compose run --rm java java -jar build/libs/Twinkle.jar parse "Test story.html"

serve:
	docker-compose up -d server
