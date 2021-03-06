% Generated by roxygen2: do not edit by hand
% Please edit documentation in R/snapshot.R
\name{snapshot.init}
\alias{snapshot.init}
\title{Initialize package}
\usage{
snapshot.init(dir, is_reference_run, equalityFunction)
}
\arguments{
\item{dir}{The directory where to store snapshot to (default: 'snapshots')}

\item{is_reference_run}{A function returning TRUE if snapshots should be taken and FALSE if values should be compared (default: snapshot dir does not exist)}

\item{equalityFunction}{The function to use for comparing actual values to snapshotted values (default: 'all.equal')}
}
\description{
Initialize package
}
\examples{
# Only comparing using snapshots in directory "my/snapshot/dir" and using function 'identical' to compare values.
snapshot.init(dir = "my/snapshot/dir", referenceRunPredicate = function() FALSE, equalityFunction = identical)

# This should do the job in most cases
snapshot.init()
}
