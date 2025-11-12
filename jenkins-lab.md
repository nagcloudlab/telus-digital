

# Stage-1 :Jenkins Installation on Ubuntu EC2 Instance

## Connecting to Your EC2 Instance via SSH
```bash
chmod 400 "kp1.pem"
ssh -i "kp1.pem" ubuntu@ec2-13-201-62-107.ap-south-1.compute.amazonaws.com
```

## Install Java (Required for Jenkins)
```bash
sudo apt update
sudo apt install openjdk-17-jdk -y
java -version
```

## Install Jenkins
```bash
sudo wget -O /etc/apt/keyrings/jenkins-keyring.asc \
  https://pkg.jenkins.io/debian-stable/jenkins.io-2023.key
echo "deb [signed-by=/etc/apt/keyrings/jenkins-keyring.asc]" \
  https://pkg.jenkins.io/debian-stable binary/ | sudo tee \
  /etc/apt/sources.list.d/jenkins.list > /dev/null
sudo apt update
sudo apt install jenkins -y
```

## Start and Enable Jenkins Service
```bash
sudo systemctl start jenkins
sudo systemctl enable jenkins
sudo systemctl status jenkins
```

## Install Maven (Optional, if you plan to use Maven builds)
```bash
sudo apt install maven -y
mvn -version
```

## Install Git (Optional, if you plan to use Git repositories)
```bash
sudo apt install git -y
git --version
```

## Access Jenkins Web Interface
1. Open your web browser and navigate to `http://<your-ec2-public-ip>:8080`
2. Retrieve the initial admin password:
```bash
sudo cat /var/lib/jenkins/secrets/initialAdminPassword
```
3. Enter the password in the Jenkins setup page to unlock Jenkins.  
4. Follow the on-screen instructions to complete the Jenkins setup, including installing recommended plugins and creating the first admin user.



# Stage-2 : Stage 2: Creating Your First Basic Pipeline