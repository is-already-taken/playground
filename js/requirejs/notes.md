
# RequireJS notes

## Resolution order

Resolution order for import paths in a module:

1. Config: packages
1. Config: paths
1. relative paths from the current module


## Base resolution

1. If `baseUrl` is defined in the config, expand `baseUrl` with the current 
   working directory and set `BASEURL` to that value
1. else `BASEURL` is `dirname(ENTRYMODULE)`


## Path expansion

1. Relative locations are referenced to the module's location 
   `dirname(CURRENTMODULE)`
1. Resolve other locations in the following order, referenced agains the 
   `BASEURL`
   1. If the name is found in `packages` resolve path by the entry module
      of that package
   1. else if the name if found in `path` resolve path by the mapped path
   1. else assume the name lies under `BASEURL`

TODO: Whats the order of <package>/<path> ?