# config after instances reset
# each

sudo -s
yum install -y amazon-efs-utils
cd /home/ec2-user/
mkdir efs
sudo mount -t nfs4 -o nfsvers=4.1,rsize=1048576,wsize=1048576,hard,timeo=600,retrans=2,noresvport fs-0c0963584ed82a37a.efs.us-east-1.amazonaws.com:/ efs

# check
df -h | grep efs

# reconfigure security group
