FROM eclipse-temurin:21-jdk

WORKDIR /app

# プロジェクトの全ファイルをコンテナ内にコピー
COPY . .

# 不要な改行コードの削除、実行権限付与、ビルド＆実行
CMD ["sh", "-c", "if [ -f gradlew ]; then sed -i 's/\r$//' gradlew && chmod +x gradlew && ./gradlew bootRun; elif [ -f mvnw ]; then sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw spring-boot:run; else apt-get update && apt-get install -y maven && mvn spring-boot:run; fi"]