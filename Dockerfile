FROM eclipse-temurin:21-jdk

WORKDIR /app

# 開発用ポートの公開 (Spring Boot のデフォルトは 8080)
EXPOSE 8080

# Windowsでの改行コード(CRLF)対策と実行権限付与を行った上で、Spring Bootを起動します
CMD ["sh", "-c", "if [ -f mvnw ]; then sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw spring-boot:run; else echo 'mvnw not found' && exit 1; fi"]
