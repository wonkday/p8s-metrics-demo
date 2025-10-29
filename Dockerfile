# Stage 1: Build minimal JRE using jlink
FROM eclipse-temurin:21-jdk-alpine AS jre_builder
LABEL maintainer=wonkday

# Create minimal JRE using jlink
RUN jlink \
    --add-modules java.base,java.logging,java.management,jdk.httpserver,jdk.management,jdk.unsupported \
    --output /jre-slim \
    --compress=2 \
    --strip-debug \
    --no-header-files \
    --no-man-pages

# Stage 2: Final runtime image
FROM alpine:3.22
LABEL maintainer=wonkday

# Set environment variables
ENV JAVA_HOME=/opt/jre-slim
ENV PATH="$JAVA_HOME/bin:$PATH"
ENV JAVA_TOOL_OPTIONS="-Xss512k -XX:+UseSerialGC"

# Copy the custom JRE from builder stage
COPY --from=jre_builder /jre-slim /opt/jre-slim

# Copy your application JAR
COPY build/libs/P8sMetricsDemo-1.0-SNAPSHOT-all.jar /app/app.jar

# Run the app
CMD ["java", "-jar", "/app/app.jar"]
