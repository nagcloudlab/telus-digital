terraform {
  required_version = ">= 1.6.0"
  
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
  
  # BACKEND COMMENTED OUT FOR WORKSHOP
  # Uncomment after creating S3 bucket if needed for production
  # backend "s3" {
  #   bucket         = "money-transfer-terraform-state-us-east-1"
  #   key            = "staging/terraform.tfstate"
  #   region         = "us-east-1"
  #   encrypt        = true
  #   dynamodb_table = "terraform-state-lock"
  # }
}

provider "aws" {
  region = var.aws_region
  
  default_tags {
    tags = {
      Environment = "staging"
      Project     = "money-transfer"
      ManagedBy   = "terraform"
    }
  }
}

# VPC
resource "aws_vpc" "main" {
  cidr_block           = "10.1.0.0/16"
  enable_dns_hostnames = true
  enable_dns_support   = true
  
  tags = {
    Name = "money-transfer-staging-vpc"
  }
}

# Public Subnets
resource "aws_subnet" "public" {
  count                   = 2
  vpc_id                  = aws_vpc.main.id
  cidr_block              = "10.1.${count.index + 1}.0/24"
  availability_zone       = data.aws_availability_zones.available.names[count.index]
  map_public_ip_on_launch = true
  
  tags = {
    Name = "money-transfer-staging-public-${count.index + 1}"
  }
}

# Private Subnets
resource "aws_subnet" "private" {
  count             = 2
  vpc_id            = aws_vpc.main.id
  cidr_block        = "10.1.${count.index + 10}.0/24"
  availability_zone = data.aws_availability_zones.available.names[count.index]
  
  tags = {
    Name = "money-transfer-staging-private-${count.index + 1}"
  }
}

# Internet Gateway
resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.main.id
  
  tags = {
    Name = "money-transfer-staging-igw"
  }
}

# Elastic IP for NAT Gateway
resource "aws_eip" "nat" {
  domain = "vpc"
  
  tags = {
    Name = "money-transfer-staging-nat-eip"
  }
}

# NAT Gateway
resource "aws_nat_gateway" "main" {
  allocation_id = aws_eip.nat.id
  subnet_id     = aws_subnet.public[0].id
  
  tags = {
    Name = "money-transfer-staging-nat"
  }
  
  depends_on = [aws_internet_gateway.main]
}

# Route Tables
resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id
  
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.main.id
  }
  
  tags = {
    Name = "money-transfer-staging-public-rt"
  }
}

resource "aws_route_table" "private" {
  vpc_id = aws_vpc.main.id
  
  route {
    cidr_block     = "0.0.0.0/0"
    nat_gateway_id = aws_nat_gateway.main.id
  }
  
  tags = {
    Name = "money-transfer-staging-private-rt"
  }
}

# Route Table Associations
resource "aws_route_table_association" "public" {
  count          = 2
  subnet_id      = aws_subnet.public[count.index].id
  route_table_id = aws_route_table.public.id
}

resource "aws_route_table_association" "private" {
  count          = 2
  subnet_id      = aws_subnet.private[count.index].id
  route_table_id = aws_route_table.private.id
}

# Security Groups
resource "aws_security_group" "alb" {
  name        = "money-transfer-staging-alb-sg"
  description = "Security group for ALB"
  vpc_id      = aws_vpc.main.id
  
  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
  
  tags = {
    Name = "money-transfer-staging-alb-sg"
  }
}

resource "aws_security_group" "ecs_tasks" {
  name        = "money-transfer-staging-ecs-sg"
  description = "Security group for ECS tasks"
  vpc_id      = aws_vpc.main.id
  
  ingress {
    from_port       = 8080
    to_port         = 8080
    protocol        = "tcp"
    security_groups = [aws_security_group.alb.id]
  }
  
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
  
  tags = {
    Name = "money-transfer-staging-ecs-sg"
  }
}

resource "aws_security_group" "rds" {
  name        = "money-transfer-staging-rds-sg"
  description = "Security group for RDS"
  vpc_id      = aws_vpc.main.id
  
  ingress {
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.ecs_tasks.id]
  }
  
  tags = {
    Name = "money-transfer-staging-rds-sg"
  }
}

# ECR Repository
resource "aws_ecr_repository" "money_transfer" {
  name                 = "money-transfer-staging"
  image_tag_mutability = "MUTABLE"
  
  image_scanning_configuration {
    scan_on_push = true
  }
  
  tags = {
    Environment = "staging"
  }
}

# ECR Lifecycle Policy
resource "aws_ecr_lifecycle_policy" "money_transfer" {
  repository = aws_ecr_repository.money_transfer.name
  
  policy = jsonencode({
    rules = [{
      rulePriority = 1
      description  = "Keep last 20 images"
      selection = {
        tagStatus     = "any"
        countType     = "imageCountMoreThan"
        countNumber   = 20
      }
      action = {
        type = "expire"
      }
    }]
  })
}

# ECS Cluster
resource "aws_ecs_cluster" "main" {
  name = "money-transfer-staging"
  
  setting {
    name  = "containerInsights"
    value = "enabled"
  }
  
  tags = {
    Environment = "staging"
  }
}

# Application Load Balancer
resource "aws_lb" "main" {
  name               = "money-transfer-staging-alb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.alb.id]
  subnets            = aws_subnet.public[*].id
  
  tags = {
    Environment = "staging"
  }
}

# Target Group
resource "aws_lb_target_group" "app" {
  name        = "money-transfer-staging-tg"
  port        = 8080
  protocol    = "HTTP"
  vpc_id      = aws_vpc.main.id
  target_type = "ip"
  
  health_check {
    enabled             = true
    healthy_threshold   = 2
    interval            = 30
    matcher             = "200"
    path                = "/actuator/health"
    port                = "traffic-port"
    protocol            = "HTTP"
    timeout             = 5
    unhealthy_threshold = 3
  }
  
  tags = {
    Environment = "staging"
  }
}

# Listener
resource "aws_lb_listener" "app" {
  load_balancer_arn = aws_lb.main.arn
  port              = "80"
  protocol          = "HTTP"
  
  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.app.arn
  }
}

# RDS Subnet Group
resource "aws_db_subnet_group" "main" {
  name       = "money-transfer-staging-db-subnet"
  subnet_ids = aws_subnet.private[*].id
  
  tags = {
    Environment = "staging"
  }
}

# RDS Instance
resource "aws_db_instance" "main" {
  identifier           = "money-transfer-staging"
  engine               = "postgres"
  engine_version       = "15.3"
  instance_class       = "db.t3.small"
  allocated_storage    = 50
  storage_encrypted    = true
  
  db_name  = "moneytransfer"
  username = var.db_username
  password = var.db_password
  
  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [aws_security_group.rds.id]
  
  backup_retention_period = 14
  backup_window          = "03:00-04:00"
  maintenance_window     = "mon:04:00-mon:05:00"
  
  skip_final_snapshot       = false
  final_snapshot_identifier = "money-transfer-staging-final-${formatdate("YYYY-MM-DD-hhmm", timestamp())}"
  
  tags = {
    Environment = "staging"
  }
}

# Secrets Manager for DB Password
resource "aws_secretsmanager_secret" "db_password" {
  name = "money-transfer-staging-db-password"
  
  tags = {
    Environment = "staging"
  }
}

resource "aws_secretsmanager_secret_version" "db_password" {
  secret_id     = aws_secretsmanager_secret.db_password.id
  secret_string = var.db_password
}

# CloudWatch Log Group
resource "aws_cloudwatch_log_group" "app" {
  name              = "/ecs/money-transfer-staging"
  retention_in_days = 14
  
  tags = {
    Environment = "staging"
  }
}

# IAM Role for ECS Task Execution
resource "aws_iam_role" "ecs_execution_role" {
  name = "money-transfer-staging-ecs-execution-role"
  
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action = "sts:AssumeRole"
      Effect = "Allow"
      Principal = {
        Service = "ecs-tasks.amazonaws.com"
      }
    }]
  })
  
  tags = {
    Environment = "staging"
  }
}

resource "aws_iam_role_policy_attachment" "ecs_execution_role_policy" {
  role       = aws_iam_role.ecs_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

resource "aws_iam_role_policy" "ecs_execution_secrets" {
  name = "ecs-execution-secrets"
  role = aws_iam_role.ecs_execution_role.id
  
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Action = [
        "secretsmanager:GetSecretValue",
        "ecr:GetAuthorizationToken",
        "ecr:BatchCheckLayerAvailability",
        "ecr:GetDownloadUrlForLayer",
        "ecr:BatchGetImage"
      ]
      Resource = "*"
    }]
  })
}

# IAM Role for ECS Task
resource "aws_iam_role" "ecs_task_role" {
  name = "money-transfer-staging-ecs-task-role"
  
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action = "sts:AssumeRole"
      Effect = "Allow"
      Principal = {
        Service = "ecs-tasks.amazonaws.com"
      }
    }]
  })
  
  tags = {
    Environment = "staging"
  }
}

# ECS Task Definition
resource "aws_ecs_task_definition" "app" {
  family                   = "money-transfer-staging"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "512"
  memory                   = "1024"
  execution_role_arn       = aws_iam_role.ecs_execution_role.arn
  task_role_arn            = aws_iam_role.ecs_task_role.arn
  
  container_definitions = jsonencode([{
    name  = "money-transfer"
    image = "${aws_ecr_repository.money_transfer.repository_url}:latest"
    
    portMappings = [{
      containerPort = 8080
      protocol      = "tcp"
    }]
    
    environment = [
      {
        name  = "SPRING_PROFILES_ACTIVE"
        value = "staging"
      },
      {
        name  = "SPRING_DATASOURCE_URL"
        value = "jdbc:postgresql://${aws_db_instance.main.endpoint}/moneytransfer"
      },
      {
        name  = "SPRING_DATASOURCE_USERNAME"
        value = var.db_username
      },
      {
        name  = "JAVA_OPTS"
        value = "-Xmx768m -Xms512m"
      }
    ]
    
    secrets = [{
      name      = "SPRING_DATASOURCE_PASSWORD"
      valueFrom = aws_secretsmanager_secret.db_password.arn
    }]
    
    logConfiguration = {
      logDriver = "awslogs"
      options = {
        "awslogs-group"         = aws_cloudwatch_log_group.app.name
        "awslogs-region"        = var.aws_region
        "awslogs-stream-prefix" = "ecs"
      }
    }
    
    healthCheck = {
      command     = ["CMD-SHELL", "wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1"]
      interval    = 30
      timeout     = 5
      retries     = 3
      startPeriod = 60
    }
  }])
  
  tags = {
    Environment = "staging"
  }
}

# ECS Service (2 tasks for HA)
resource "aws_ecs_service" "app" {
  name            = "money-transfer-staging"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.app.arn
  desired_count   = 2
  launch_type     = "FARGATE"
  
  network_configuration {
    subnets          = aws_subnet.private[*].id
    security_groups  = [aws_security_group.ecs_tasks.id]
    assign_public_ip = false
  }
  
  load_balancer {
    target_group_arn = aws_lb_target_group.app.arn
    container_name   = "money-transfer"
    container_port   = 8080
  }
  
  deployment_configuration {
    maximum_percent         = 200
    minimum_healthy_percent = 100
  }
  
  depends_on = [aws_lb_listener.app]
  
  tags = {
    Environment = "staging"
  }
}

# Data Sources
data "aws_availability_zones" "available" {
  state = "available"
}
