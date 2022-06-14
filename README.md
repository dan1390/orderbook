### Launch application

1. Start up local db instance by running `docker-compose up -d` in the project root
2. Launch application by running `./mvnw spring-boot:run` in the project root
    - Or by starting it from your IDE
3. Go nuts   
4. Don't forget to shut down db instance afterwards by running `docker-compose down --remove-orphans`

### Run tests
Run `./mvnw clean test` in project root
