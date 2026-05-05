FROM maven:3.9-eclipse-temurin-17

RUN apt-get update && apt-get install -y --no-install-recommends \
    python3 python3-pip nodejs npm \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /opt/rnatango

# Layer 1: Python dependencies (rarely change)
COPY requirements.txt .
RUN pip3 install --no-cache-dir --break-system-packages -r requirements.txt

# Layer 2: Clone and build external dependencies (rarely change)
ADD https://github.com/tzok/varna-tz.git /tmp/varna-tz
RUN mvn -f /tmp/varna-tz/pom.xml install -DskipTests

ADD https://github.com/tzok/mcq4structures.git /tmp/mcq4structures
RUN mvn -f /tmp/mcq4structures/pom.xml install -DskipTests

# Layer 3: Maven dependencies (rarely change)
COPY engine/pom.xml engine/
RUN mvn dependency:go-offline -f engine/pom.xml

# Layer 4: Node.js dependencies (rarely change)
COPY rnatango-frontend/package*.json rnatango-frontend/
RUN cd rnatango-frontend && npm install

# Layer 5: Application source code (changes frequently)
COPY . .

# Layer 6: Build frontend
RUN cd rnatango-frontend \
    && npm run build \
    && mkdir -p /opt/rnatango/frontend \
    && cp -r out/* /opt/rnatango/frontend/

# Layer 7: Build backend (tests included)
RUN cd engine && mvn package

CMD ["sh", "-c", "mkdir -p /shared/frontend && cp -r /opt/rnatango/frontend/* /shared/frontend/ && exec java -jar /opt/rnatango/engine/target/rnatango-engine-1.0.1-RELEASE.jar"]