#THIS IS A TEMPLATE - CHANGE AT YOUR OWN RISK!

#establish error logging (Taken from: https://stackoverflow.com/questions/11666086/)
zz <- file("Errorlog.Rout", open="wt")
sink(zz, type="message")

library(ggplot2)

#Don't manipulate this line - it is automatically changed by the program to point at the current output file!
filename <- REPLACEME

results <- read.table(filename, sep=",", row.names=1, header=TRUE)
results <- within(results, Sites.ratio <- Sites.successful/Sites.total*100)
results <- within(results, Space.ratio <- Space.used/Space.total*100)
results <- within(results, TimeToSites <- Routing.Time/Sites.total)
results <- within(results, SpaceToSites <- Space.used/Sites.total)
results <- within(results, TimeTotal <- Graph.Time+Routing.Time)

plot(results[,'Graph.Time'], ylab="Time (ns)", type="b", main="Plot of time elapsed for Graph construction")
plot(results[,'Routing.Time'], ylab="Time (ns)", type="b", main="Plot of time elapsed for Routing")
boxplot(results$Routing.Time, ylab="Time (ns)", main="Routing: Time elapsed (Outliers removed)", outline=FALSE)
boxplot(results$TimeToSites, main="Average time per site (Outliers removed)", outline=FALSE)
boxplot(results$Sites.ratio, main="Percentage of successful routings")
boxplot(results$Space.ratio, main="Percentage of Label Area usage")
a <- ggplot(results)
a <- a+geom_bar(aes(factor(results$Sites.successful)),fill="lightgoldenrod2")
a <- a+stat_boxplot(aes(factor(results$Sites.successful),results$Space.ratio), geom="errorbar")#Workaround to get T-shaped whiskers
a <- a+geom_boxplot(aes(factor(results$Sites.successful),results$Space.ratio))
a <- a+xlab("Number of successful routings")+ylab("Results per category              Space usage (%)       ")+ggtitle("Labeling area space usage by routings")

a



#plot(results$Sites.successful,results$Space.ratio, xlab="Successfully routed sites", ylab="Label area space used (%)", main="Label area usage by number of successful routings")




## reset message sink and close the file connection
sink(type="message")
close(zz)

#User feedback
message("Maximum Values for:")
colMax <- function(data) sapply(data, max, na.rm = TRUE)
colMax(results)
message("Calculations completed successfully.")
message("Please do not close the window prematurely.")#Closing the shell window early seems to corrupt the PDF with the plots
Sys.sleep(1)
